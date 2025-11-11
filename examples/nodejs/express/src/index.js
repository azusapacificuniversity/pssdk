const AppServer = Java.type('edu.apu.pssdk.AppServer');
const express = require('express');
const helmet = require('helmet');
require('dotenv').config();

const app = express();
app.set('trust proxy', true);
app.set('json spaces', 2);

const appServer = AppServer.fromEnv();
const ci = (ciName, opts = {}) => {
  const ci = appServer.ciFactory(ciName, opts);
  return {
    get: (params) => Promise.resolve(ci.get(params)),
    set: (params) => Promise.resolve(ci.set(params)),
    find: (params) => Promise.resolve(ci.find(params)),
    create: (params) => Promise.resolve(ci.create(params)),
    save: (params) => Promise.resolve(ci.save(params)),
    cancel: () => Promise.resolve(ci.cancel()),
    insert: (params) => Promise.resolve(ci.insert(params)),
    update: (params) => Promise.resolve(ci.update(params)),
    execute: (methodName, params) => Promise.resolve(ci.execute(methodName, params)),
  };
};


app.get('/api/v1/profiles/:userid', (req, res) => {
  const { userid } = req.params;
  if (!userid) {
    return res.status(400).send('userid parameter is required.');
  }
  ci('USER_PROFILE')
    .get({ 'UserID': userid})
    .then((user) => res.json(user));
});

// start listening to port 3000 through express
app.listen(3000, () => console.log('pssdk-express server started'));

