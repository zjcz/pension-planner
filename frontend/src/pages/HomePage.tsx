import { useNavigate } from 'react-router-dom';
import { Button } from 'primereact/button';
import { Toolbar } from 'primereact/toolbar';
import { useAuth } from '../auth/AuthContext';

export default function HomePage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const onLogout = async () => {
    await logout();
    navigate('/login', { replace: true });
  };

  const start = (
    <div className="flex align-items-center gap-2">
      <span className="pi pi-home mr-2" />
      <span className="font-bold">Pension Planner</span>
    </div>
  );

  const end = (
    <div className="flex align-items-center gap-3">
      <span className="text-secondary">Signed in as {user?.username}</span>
      <Button label="Log Out" icon="pi pi-sign-out" severity="secondary" onClick={onLogout} />
    </div>
  );

  return (
    <div>
      <Toolbar start={start} end={end} />
      <div className="p-4">
        <h2>Welcome, {user?.username}</h2>
        <p className="text-secondary">The dashboard will be built in Phase 2.</p>
      </div>
    </div>
  );
}
