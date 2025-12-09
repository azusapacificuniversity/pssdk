module.exports = {
  get: ({ ci, params }) => ci.get(params),
  set: ({ ci, params }) => ci.set(params),
  find: ({ ci, params }) => ci.find(params),
  create: ({ ci, params }) => ci.create(params),
  save: ({ ci, params }) => ci.save(params),
  cancel: ({ ci }) => ci.cancel(),
  execute: ({ ci, methodName, params }) => ci.execute(methodName, params),
  insert: ({ ci, params }) => ci.create(params).save(params),
};

