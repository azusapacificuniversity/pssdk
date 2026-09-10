const { AppServer } = require('pssdk');
const express = require('express');
const helmet = require('helmet');
require('dotenv').config();

const app = express();
app.use(helmet());
app.set('trust proxy', true);
app.set('json spaces', 2);

const appServer = AppServer.fromEnv();

app.get('/api/v1/profiles/:userid', (req, res, next) => {
  const { userid } = req.params;
  if (!userid) {
    return res.status(400).send('userid parameter is required.');
  }
  // `fetch` does a get, converts the CI to a plain object, and closes the session
  return appServer.ci('USER_PROFILE')
    .fetch({ UserID: userid })
    .then((user) => res.json(user))
    .catch(next);
});

// start listening to port 3000 through express
app.listen(3000, () => console.log('pssdk-express server started'));
