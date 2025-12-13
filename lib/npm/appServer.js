// eslint-disable-next-line no-undef
const JavaAppServer = Java.type('edu.apu.pssdk.AppServer');
const Piscina = require('piscina');
const path = require('path');
const ThreadableCI = require('./threadableCI.js');

class AppServer {
  constructor(_appServerConfig, _logger = console) {
    this.appServer = new JavaAppServer(_appServerConfig);
    this.logger = _logger;
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

}

module.exports = AppServer;
