function setup() {
  const authentication = Java.type('nva.api.testing.AuthenticationMethods');
  const username = karate.properties['username'];
  const client_id = karate.properties['clientId'];
  const user_pool_id = karate.properties['userPoolId'];

  return {
    BEARER_TOKEN: authentication.getIdToken(username, client_id, user_pool_id),
    SERVER_URL: karate.properties['serverUrl']
  }
}