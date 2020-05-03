print("Setting up admin users");

db = db.getSiblingDB('admin');
db.createUser(
  {
    user: "rackdon",
    pwd: "rackdon",
    roles: [ { role: "root", db: "admin" } ]
  }
);

