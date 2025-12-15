// eslint-disable-next-line no-undef
const JavaAppServer = Java.type('edu.apu.pssdk.AppServer');
const Piscina = require('piscina');
const path = require('path');
const ThreadableCI = require('./threadableCI.js');

class AppServer {
  constructor(_appServerConfig) {
    this.appServer = typeof _appServerConfig === JavaAppServer
      ? _appServerConfig
      : new JavaAppServer(_appServerConfig);
    this.pool = new Piscina({
      filename: path.resolve(__dirname, 'worker.js'),
      idleTimeout: 60000,
    });
  }

  ci(ciName, opts = {}) {
    return new ThreadableCI(
      this.appServer.ciFactory(ciName, opts),
      this.pool,
    );
  }

  static fromEnv() {
    return new AppServer(JavaAppServer.fromEnv());
  }
}

module.exports = AppServer;
