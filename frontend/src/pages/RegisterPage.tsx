import { useEffect, useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { Button } from 'primereact/button';
import { InputText } from 'primereact/inputtext';
import { Password } from 'primereact/password';
import { Message } from 'primereact/message';
import { authApi } from '../api/auth';
import { apiErrorMessage } from '../api/client';
import { useAuth } from '../auth/AuthContext';

export default function RegisterPage() {
  const { user, register } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const { data: config } = useQuery({
    queryKey: ['auth', 'config'],
    queryFn: () => authApi.config(),
    staleTime: Infinity,
  });

  useEffect(() => {
    if (user) {
      navigate('/', { replace: true });
    }
  }, [user, navigate]);

  if (config && !config.allowRegistration) {
    return (
      <div className="flex align-items-center justify-content-center min-h-screen">
        <div className="card p-4 text-center" style={{ width: '28rem' }}>
          <h2 className="m-0">Pension Planner</h2>
          <p className="mt-3">Registration is currently disabled by the administrator.</p>
          <Link to="/login">Back to Sign In</Link>
        </div>
      </div>
    );
  }

  const onSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    if (password !== confirmPassword) {
      setError('Passwords do not match');
      return;
    }
    setSubmitting(true);
    try {
      await register(username, password);
      navigate('/', { replace: true });
    } catch (err) {
      setError(apiErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="flex align-items-center justify-content-center min-h-screen">
      <div className="card p-4" style={{ width: '28rem' }}>
        <div className="text-center mb-4">
          <h2 className="m-0">Pension Planner</h2>
          <span className="text-secondary text-sm">Create your account</span>
        </div>

        <form onSubmit={onSubmit} className="flex flex-column gap-3">
          <div className="flex flex-column gap-2">
            <label htmlFor="username">Username</label>
            <InputText
              id="username"
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              autoComplete="username"
              maxLength={50}
              required
            />
          </div>

          <div className="flex flex-column gap-2">
            <label htmlFor="password">Password</label>
            <Password
              id="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              className="w-full"
              inputClassName="w-full"
              toggleMask
              autoComplete="new-password"
              minLength={8}
              required
            />
          </div>

          <div className="flex flex-column gap-2">
            <label htmlFor="confirmPassword">Confirm Password</label>
            <Password
              id="confirmPassword"
              value={confirmPassword}
              onChange={(event) => setConfirmPassword(event.target.value)}
              className="w-full"
              inputClassName="w-full"
              toggleMask
              feedback={false}
              autoComplete="new-password"
              required
            />
          </div>

          {error && <Message severity="error" text={error} />}

          <Button type="submit" label="Create Account" icon="pi pi-user-plus" loading={submitting} />
        </form>

        <div className="mt-3 text-center">
          <span className="text-secondary text-sm">Already have an account? </span>
          <Link to="/login">Sign In</Link>
        </div>
      </div>
    </div>
  );
}
