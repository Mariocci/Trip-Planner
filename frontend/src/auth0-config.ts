/**
 * Auth0 configuration for the frontend application.
 */
export const auth0Config = {
  domain: 'dev-dhh7rrc7jjza3i2w.eu.auth0.com',
  clientId: 'YeQ95SUX9ck94P6So4ixkT0sli5WAnUR',
  authorizationParams: {
    redirect_uri: window.location.origin + '/callback',
    audience: 'https://tripplanner-api',
    scope: 'openid profile email'
  }
};
