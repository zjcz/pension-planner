import { Message } from 'primereact/message';

interface PageErrorProps {
  message: string;
}

export function PageError({ message }: PageErrorProps) {
  return (
    <div className="p-4">
      <Message severity="error" text={message} className="w-full" />
    </div>
  );
}
