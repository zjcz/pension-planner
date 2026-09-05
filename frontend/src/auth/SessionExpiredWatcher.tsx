import { useEffect, useState } from 'react';
import type { createBrowserRouter } from 'react-router-dom';
import { setSessionExpiredHandler } from '../api/client';
import { useAuth } from '../auth/AuthContext';
import { SessionExpiredDialog } from '../components/SessionExpiredDialog';

interface SessionExpiredWatcherProps {
  router: ReturnType<typeof createBrowserRouter>;
}

export function SessionExpiredWatcher({ router }: SessionExpiredWatcherProps) {
  const { markSessionExpired } = useAuth();
  const [sessionExpired, setSessionExpired] = useState(false);

  useEffect(() => {
    const handle = () => {
      markSessionExpired();
      setSessionExpired(true);
    };
    setSessionExpiredHandler(handle);
    return () => setSessionExpiredHandler(null);
  }, [markSessionExpired]);

  const handleSignIn = () => {
    setSessionExpired(false);
    router.navigate('/login', { replace: true });
  };

  return (
    <SessionExpiredDialog
      visible={sessionExpired}
      onHide={() => setSessionExpired(false)}
      onSignIn={handleSignIn}
    />
  );
}
