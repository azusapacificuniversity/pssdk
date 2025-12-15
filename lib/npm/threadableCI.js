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

  execute(methodName, params) {
    return this.pool
      .run({ ci: this.ci, methodName, params }, { name: 'execute' })
      .then(() => this);
  }

  toJSON() {
    return this.ci.toJSON();
  }

  toArray() {
    return this.ci.toList();
  }

  setInteractiveMode(params) {
    this.ci.setInteractiveMode(params);
    return this;
  }

  cancel() {
    return this.ci.cancel();
  }

  /*********************************/
  /******** HELPER METHODS *********/
  /*********************************/
  /**
   * Helper method to create and then save a new object.
   *
   * @param data The data for the new object.
   * @return a resulting object that could be JSON stringified.
   * @throws PssdkException If a PSDK specific error occurs.
   */
  add(params) {
    return Promise.resolve(params)
    .then(() => this.create(params))
    .then(() => this.save(params))
    .then(() => this.toJSON())
    .finally(() => this.cancel());
  }

  /**
   * Helper method to get a new object and return its data.
   *
   * @param data an object containing the GET keys/values.
   * @return a resulting object that could be JSON stringified.
   * @throws PssdkException If a PSDK specific error occurs.
   */
  fetch(params) {
    return Promise.resolve(params)
    .then(() => this.get(params))
    .then(() => this.toJSON())
    .finally(() => this.cancel());
  }

  /**
   * Helper method to get and then save an object.
   *
   * @param The data for the object to update.
   * @return a resulting object that could be JSON stringified.
   * @throws PssdkException If a PSDK specific error occurs.
   */
  update(params) {
    return Promise.resolve(params)
    .then(() => this.get(params))
    .then(() => this.save(params))
    .then(() => this.toJSON())
    .finally(() => this.cancel());
  }

  /**
   * Helper method to do a find and return the resulting js array
   *
   * @param an object containing the FIND keys/values.
   * @return a resulting array that could be JSON stringified.
   * @throws PssdkException If a PSDK specific error occurs.
   */
  search(params) {
    return Promise.resolve(params)
    .then(() => this.find(params))
    .then(() => this.toArray())
    .finally(() => this.cancel());
  }
}

module.exports = TreadableCI;
