import { ProgressSpinner } from 'primereact/progressspinner';

export function PageLoading() {
  return (
    <div className="flex flex-col items-center justify-center p-12 gap-4">
      <ProgressSpinner />
      <span className="text-secondary">Loading...</span>
    </div>
  );
}
