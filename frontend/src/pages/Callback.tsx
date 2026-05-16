import { useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';

/**
 * Callback page that Auth0 redirects to after login.
 * Handles the authentication flow and redirects to home.
 */
export default function Callback() {
  const { isLoading, error } = useAuth0();

  useEffect(() => {
    if (!isLoading && !error) {
      // Auth0 will automatically handle the redirect
      window.location.href = '/';
    }
  }, [isLoading, error]);

  if (error) {
    return (
      <div style={{ textAlign: 'center', padding: '50px' }}>
        <h2>Authentication Error</h2>
        <p>{error.message}</p>
        <button onClick={() => window.location.href = '/'}>
          Go Home
        </button>
      </div>
    );
  }

  return (
    <div style={{ textAlign: 'center', padding: '50px' }}>
      <h2>Completing login...</h2>
      <p>Please wait while we redirect you.</p>
    </div>
  );
}
