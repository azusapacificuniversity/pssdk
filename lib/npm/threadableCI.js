class TreadableCI {
  constructor(ci, pool) {
    this.pool = pool;
    this.ci = ci;
  }

  get(params) {
    return this.pool
      .run({ ci: this.ci, params }, { name: 'get' })
      .then(() => this);
  }

  set(params) {
    return this.pool
      .run({ ci: this.ci, params }, { name: 'set' })
      .then(() => this);
  }

  find(params) {
    return this.pool
      .run({ ci: this.ci, params }, { name: 'find' })
      .then(() => this);
  }

  create(params) {
    return this.pool
      .run({ ci: this.ci, params }, { name: 'create' })
      .then(() => this);
  }

  save(params) {
    return this.pool
      .run({ ci: this.ci, params }, { name: 'save' })
      .then(() => this);
  }

  cancel() {
    return this.pool
      .run({ ci: this.ci }, { name: 'cancel' })
      .then(() => this);
  }

  execute(methodName, params) {
    return this.pool
      .run({ ci: this.ci, methodName, params }, { name: 'execute' })
      .then(() => this);
  }

  toJSON() {
    return this.ci.toJSON();
  }

  setInteractiveMode(params) {
    this.ci.setInteractiveMode(params);
    return this;
  }

  insert(params) {
    return this.pool
      .run({ ci: this.ci, params }, { name: 'insert' })
      .then(() => this.toJSON());
  }
}

module.exports = TreadableCI;
