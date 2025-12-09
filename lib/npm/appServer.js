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

  ping() {
    return this.ci('APU_APPLICATION_UPDATE')
      .get({
        ADM_APPL_NBR: '00275505',
        EMPLID: '001610517',
        ACAD_CAREER: 'GR',
        INSTITUTION: 'APU',
      })
      .then(() => 'pong');
  }
}

module.exports = AppServer;
