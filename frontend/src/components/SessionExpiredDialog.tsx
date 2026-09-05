import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';

interface SessionExpiredDialogProps {
  visible: boolean;
  onHide: () => void;
  onSignIn: () => void;
}

export function SessionExpiredDialog({ visible, onHide, onSignIn }: SessionExpiredDialogProps) {
  return (
    <Dialog
      header="Session Expired"
      visible={visible}
      closable={false}
      closeOnEscape={false}
      onHide={onHide}
      modal
      style={{ width: '28rem' }}
    >
      <p className="mt-0">Your session has expired. Please sign in again to continue.</p>
      <div className="flex justify-content-end">
        <Button label="Sign In" icon="pi pi-sign-in" onClick={onSignIn} autoFocus />
      </div>
    </Dialog>
  );
}
