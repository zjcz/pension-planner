import { ProgressSpinner } from 'primereact/progressspinner';

export function PageLoading() {
  return (
    <div className="flex flex-column align-items-center justify-content-center p-6 gap-3">
      <ProgressSpinner />
      <span className="text-secondary">Loading...</span>
    </div>
  );
}
