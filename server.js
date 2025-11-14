const express = require('express');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const cookieParser = require('cookie-parser');
const axios = require('axios');
const path = require('path');
const app = express();
var url = require('url')
const router = express.Router();
const crypto = require('crypto');
const bodyParser = require('body-parser');
const {MongoClient} = require('mongodb');
require('dotenv').config();
const JWT_SECRET = "f7c0019e03c5e87964f56a0d2ca7e18a878ff96d6afa247c5c53cf39d21873eddd8bb78477e9229501fd16bc8ecc9e107083a43a6337c61cff76dd5b8f91b995";
const cors = require('cors');
app.use(cors());

app.use(bodyParser.json());
app.use(cookieParser());
app.use(express.json());

app.use((req, res, next) => {
  console.log('Middleware Check - Headers:', req.headers);
  console.log('Middleware Check - Body:', req.body);
  next();
});

var mdburl = "mongodb+srv://chitkala2001:FLPdPZMXj3ZyN8TV@cluster0.okm8opu.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";


const CREDENTIALS = {
  client_id: '257d8d8b76d7f16db010',
  client_secret: '09e93a43f366def113dddf18e4c75391',
};

const ENDPOINTS = {
  AUTH_URL: 'https://api.artsy.net/api/tokens/xapp_token',
  SEARCH_URL: 'https://api.artsy.net/api/search',
  ARTISTS_URL: 'https://api.artsy.net/api/artists/',
  ARTWORK_URL:  'https://api.artsy.net/api/artworks',
  GENES_URL:  'https://api.artsy.net/api/genes' 
};

let tokenData = {
  "type" : "xapp_token",
  "token" : "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6IiIsInN1YmplY3RfYXBwbGljYXRpb24iOiI1MzBhMzEzNC05MTJkLTQ0Y2YtYjFhZi1iOGNiYjcyNjk1NzAiLCJleHAiOjE3NDE0ODQ2NTIsImlhdCI6MTc0MDg3OTg1MiwiYXVkIjoiNTMwYTMxMzQtOTEyZC00NGNmLWIxYWYtYjhjYmI3MjY5NTcwIiwiaXNzIjoiR3Jhdml0eSIsImp0aSI6IjY3YzNiN2VjMWM5NzIwNDVlOTgxNjc1MyJ9.d5ApOjY5oyWPYhlseBnBvpsnMsu7mJNnkYQf1f8fuIg",
  "expires_at" : "2025-02-22T19:33:31+00:00"
};


// Token update function
async function tokenUpdate() {
  console.log("Current token:", tokenData.token);
  console.log("Using credentials:", CREDENTIALS);

  try {
    const response = await axios.post(ENDPOINTS.AUTH_URL, null, {
      params: CREDENTIALS
    });

    if (response.status === 200 || response.status === 201) {
      const tokenResponse = response.data;
      tokenData.token = tokenResponse.token;
      tokenData.expires_at = tokenResponse.expires_at;
      console.log("Token updated:", tokenData.token);
      return tokenData.token;
    } else {
      console.error("Invalid request", response.status, response.statusText);
      return null;
    }
  } catch (error) {
    console.error("Error updating token:", error.message);
    return null;
  }
}

// endpoint to retrieve token 
app.get('/token', async (req, res) => {
  const token = await tokenUpdate();
  if (token) {
    res.json({
      token,
      expires_at: tokenData.expires_at
    });
  } else {
    res.status(500).json({ error: 'Failed to retrieve or update token' });
  }
});

// Route to search artists
app.get('/search/:artist', async (req, res) => {
  const artistQuery = req.params.artist;
  console.log(artistQuery);

  if (!artistQuery) {
    return res.status(400).json({ error: 'Query parameter is required' });
  }
  const token = await tokenUpdate();
  
  try {
    const response = await axios.get(ENDPOINTS.SEARCH_URL, {
      headers: {
        'X-XAPP-Token': token
      },
      params: {
        q: artistQuery,
        size: 10,
        type: 'artist'
      }
    });

    const artistsData = response.data._embedded?.results || [];

    const results = artistsData.map(artist => ({
      id: artist._links.self.href.split('/').pop(),
      name: artist.title || 'Unknown',
      imageUrl: artist._links.thumbnail.href === 'assets/artsy_logo.svg'? null : artist._links.thumbnail.href
    }));

    res.json({ results });

  } catch (err) {
    console.error('Failed to retrieve artists:', err.message);
    res.status(500).json({ error: 'Failed to retrieve artists' });
  }
});

// Route to fetch artist details by ID
app.get('/artists/:id', async (req, res) => {
  const artistId = req.params.id;
  if (!artistId) {
    return res.status(400).json({ error: 'Artist ID is required' });
  }

  const token = await tokenUpdate();
  
  try {
    const response = await axios.get(`${ENDPOINTS.ARTISTS_URL}${artistId}`, {
      headers: {
        'X-XAPP-Token': token
      }
    });

    const artist = response.data;
    res.json({
      name: artist.name || ' ',
      birthday: artist.birthday || ' ',
      deathday: artist.deathday || ' ',
      nationality: artist.nationality || ' ',
      biography: artist.biography || ''
    });

  } catch (err) {
    const status = err.response?.status || 500;
    const message = err.response?.data || 'Failed to fetch artist details';
    console.error("Error fetching artist details:", err.message);
    res.status(status).json({ error: message });
  }
});

// Route to fetch artworks for a given artist ID
app.get('/artworks/:artist_id', async (req, res) => {
  const artistId = req.params.artist_id;
  if (!artistId) {
    return res.status(400).json({ error: 'Artist ID is required' });
  }
  const token = await tokenUpdate(); 
  try {
    const response = await axios.get(ENDPOINTS.ARTWORK_URL, {
      headers: {
        'X-XAPP-Token': token
      },
      params: {
        artist_id: artistId,
        size: 10
      }
    });
    const artworks = response.data._embedded?.artworks || [];
    const results = artworks.map(artwork => ({
      id: artwork.id || 'N/A',
      title: artwork.title || 'Untitled',
      date: artwork.date || ' ',
      image: artwork._links?.thumbnail?.href || null
    }));

    res.json({ results });

  } catch (err) {
    const status = err.response?.status || 500;
    const message = err.response?.data || 'Failed to fetch artworks';
    console.error("Error fetching artworks:", err.message);
    res.status(status).json({ error: message });
  }
});

app.get('/genes/:artwork_id', async (req, res) => {
  const artworkId = req.params.artwork_id;
  if (!artworkId) {
    return res.status(400).json({ error: 'Artwork ID is required' });
  }

  try {
    const token = await tokenUpdate();

    // Step 1: Get artwork details to retrieve the genes URL
    const artworkRes = await axios.get(`https://api.artsy.net/api/artworks/${artworkId}`, {
      headers: {
        'X-XAPP-Token': token
      }
    });

    const genesUrl = artworkRes.data._links.genes.href;

    // Step 2: Fetch genes from the genes URL
    const genesRes = await axios.get(genesUrl, {
      headers: {
        'X-XAPP-Token': token
      }
    });

    const genes = genesRes.data._embedded?.genes || [];

    const results = genes.map(gene => ({
      id: gene.id,
      name: gene.name || 'Unnamed',
      image: gene._links?.thumbnail?.href || null,
      description: gene.description || 'No description available.'
    }));

    console.log("Sending categories to frontend:", results);
    res.json(results);

  } catch (err) {
    const status = err.response?.status || 500;
    const message = err.response?.data || 'Failed to fetch categories';
    console.error("Error fetching categories:", err.message);
    res.status(status).json({ error: message });
  }
});


// Route to fetch similar artists
app.get('/artists/:artist_id/similar', async (req, res) => {
  const artistId = req.params.artist_id;
  if (!artistId) {
    return res.status(400).json({ error: 'Artist ID is required' });
  }

  const token = await tokenUpdate();

  try {
    const response = await axios.get('https://api.artsy.net/api/artists', {
      headers: {
        'X-XAPP-Token': token
      },
      params: {
        similar_to_artist_id: artistId,
        size: 10
      }
    });

    const similarArtists = response.data._embedded?.artists || [];

    const results = similarArtists.map(artist => ({
      id: artist.id,
      name: artist.name || 'Unknown',
      imageUrl: artist._links?.thumbnail?.href || null
    }));

    res.json({ results });

  } catch (err) {
    const status = err.response?.status || 500;
    const message = err.response?.data || 'Failed to fetch similar artists';
    console.error("Error fetching similar artists:", err.message);
    res.status(status).json({ error: message });
  }
});



app.post('/register', async function (req, res) {
  const { name, email, password } = req.body; 

  console.log("backend data", req.body);
  console.log(name, email, password);

  const client = new MongoClient(mdburl);
  try {
    await client.connect();
    const db = client.db("Artsy");
    const users = db.collection("HW3");

    const existing = await users.findOne({ email: email.toLowerCase() });
    if (existing) {
      return res.status(400).json({ errors: { email: 'Email is already registered.' } });
    }

    const hashedPassword = await bcrypt.hash(password, 10);
    const hash = crypto.createHash('md5').update(email.trim().toLowerCase()).digest('hex');
    const profileImageUrl = `https://www.gravatar.com/avatar/${hash}?d=identicon`;

    const newUser = {
      name,
      email: email.toLowerCase(),
      password: hashedPassword,
      profileImageUrl
    };

    await users.insertOne(newUser);
    const token = jwt.sign({ email }, JWT_SECRET, { expiresIn: '1h' });
    res.cookie('token', token, { httpOnly: true, maxAge: 3600000 });

    res.status(201).json({ message: 'User registered successfully.',
      profile: {
        name: newUser.name,
        email: newUser.email,
        profileImageUrl: newUser.profileImageUrl
      }
     });
  } catch (err) {
    console.error(err);
    res.status(500).json({ errors: { general: String(err.message) } });
  } finally {
    await client.close();
  }
});


app.post('/login', async function (req, res) {
  const { email, password } = req.body;

  const client = new MongoClient(mdburl);
  try {
    await client.connect();
    const db = client.db("Artsy");
    const users = db.collection("HW3");

    const user = await users.findOne({ email: email.toLowerCase() });
    if (!user) {
      return res.status(400).json({ errors: { general: 'Username or password is incorrect' } });
    }

    const isMatch = await bcrypt.compare(password, user.password);
    if (!isMatch) {
      return res.status(400).json({ errors: { general: 'Username or password is incorrect' } });
    }

    
    if (!user.profileImageUrl) {
      const hash = crypto.createHash('md5').update(email.trim().toLowerCase()).digest('hex');
      const profileImageUrl = `https://www.gravatar.com/avatar/${hash}?d=identicon`;

      await users.updateOne(
        { email: email.toLowerCase() },
        { $set: { profileImageUrl } }
      );

      user.profileImageUrl = profileImageUrl;
    }

    const token = jwt.sign({ email }, JWT_SECRET, { expiresIn: '1h' });
    res.cookie('token', token, { httpOnly: true, maxAge: 3600000 });
    console.log("Token sent in cookie:", token);

    res.status(200).json({
      message: 'Login successful',
      profile: {
        name: user.name,
        email: user.email,
        profileImageUrl: user.profileImageUrl  
      }
    });

  } catch (err) {
    console.error(err);
    res.status(500).json({ errors: { general: 'Internal server error' } });
  } finally {
    await client.close();
  }
});

app.get('/me', async (req, res) => {
  const token = req.cookies.token;
  if (!token) return res.status(401).json({ error: 'Unauthorized' });

  try {
    const decoded = jwt.verify(token, JWT_SECRET);
    console.log("🔐 [GET /me] Authenticated as:", decoded.email);
    const client = new MongoClient(mdburl);
    await client.connect();
    const db = client.db("Artsy");
    const users = db.collection("HW3");
    const user = await users.findOne({ email: decoded.email });

    if (!user) {
      return res.status(401).json({ error: 'User not found' });
    }

    res.json({
      name: user.name,
      email: user.email,
      profileImageUrl: user.profileImageUrl
    });

  } catch (err) {
    console.error(err);
    res.status(401).json({ error: 'Unauthorized' });
  }
});

app.post('/logout', (req, res) => {
  res.clearCookie('token');
  res.status(200).json({ message: 'Logged out successfully' });
});

app.delete('/delete', async (req, res) => {
  const token = req.cookies.token;
  if (!token) return res.status(401).json({ error: 'Unauthorized' });

  try {
    const decoded = jwt.verify(token, JWT_SECRET);
    const client = new MongoClient(mdburl);
    await client.connect();
    const db = client.db("Artsy");
    const users = db.collection("HW3");
    const favorites = db.collection("Favorites"); 

    await users.deleteOne({ email: decoded.email });
    await favorites.deleteMany({ email: decoded.email });

    res.clearCookie('token');
    res.status(200).json({ message: 'Account deleted successfully' });

  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error deleting account' });
  }
});


app.post('/favorites', async (req, res) => {
  const token = req.cookies.token;
  if (!token) return res.status(401).json({ error: 'Unauthorized' });

  const artist = req.body;

  try {
    const decoded = jwt.verify(token, JWT_SECRET);
    const client = new MongoClient(mdburl);
    await client.connect();
    const db = client.db("Artsy");
    const favorites = db.collection("Favorites");

    // ✅ Get Artsy API token once
    const artsyToken = await tokenUpdate();

    // 🔍 Fetch full artist details from Artsy
    const fullArtistRes = await axios.get(`${ENDPOINTS.ARTISTS_URL}${artist.id}`, {
      headers: {
        'X-XAPP-Token': artsyToken
      }
    });

    const fullArtist = fullArtistRes.data;

    const data = {
      email: decoded.email,
      artistId: artist.id,
      name: fullArtist.name || artist.name || 'Unknown',
      birthday: fullArtist.birthday || '',
      deathday: fullArtist.deathday || '',
      nationality: fullArtist.nationality || '',
      imageUrl: artist.imageUrl,
      addedAt: new Date()
    };

    console.log("✅ Saving favorite:", data);
    await favorites.insertOne(data);
    res.status(201).json({ message: 'Added to favorites' });

  } catch (err) {
    console.error("❌ Error adding favorite:", err);
    res.status(500).json({ error: 'Failed to add to favorites' });
  }
});



app.delete('/favorites/:id', async (req, res) => {
  const token = req.cookies.token;
  if (!token) return res.status(401).json({ error: 'Unauthorized' });

  const artistId = req.params.id;

  try {
    const decoded = jwt.verify(token, JWT_SECRET);
    const client = new MongoClient(mdburl);
    await client.connect();
    const db = client.db("Artsy");
    const favorites = db.collection("Favorites");

    await favorites.deleteOne({ email: decoded.email, artistId: artistId });
    res.status(200).json({ message: 'Removed from favorites' });

  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Failed to remove from favorites' });
  }
});

app.get('/favorites', async (req, res) => {
  const token = req.cookies.token;
  if (!token) return res.status(401).json({ error: 'Unauthorized' });

  try {
    const decoded = jwt.verify(token, JWT_SECRET);
    console.log("🍪 [GET /favorites] Authenticated as:", decoded.email);
    const client = new MongoClient(mdburl);
    await client.connect();
    const db = client.db("Artsy");
    const favorites = db.collection("Favorites");

    const results = await favorites.find({ email: decoded.email }).sort({ addedAt: -1 }).toArray();
    res.json(results);

  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Failed to fetch favorites' });
  }
});


const port = process.env.PORT || 8080;
app.listen(port, () => {
  console.log(`Server listening on http://localhost:${port}`);
});

