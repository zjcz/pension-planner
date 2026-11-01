import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';

interface WelcomeDialogProps {
  visible: boolean;
  onHide: () => void;
}

export function WelcomeDialog({ visible, onHide }: WelcomeDialogProps) {
  return (
    <Dialog
      header="Welcome to Pension Planner"
      visible={visible}
      closable
      closeOnEscape
      onHide={onHide}
      modal
      style={{ width: '28rem' }}
    >
      <p className="mt-0 mb-3.5">
        Welcome aboard! Your Pension Planner account has been created and you're all set up.
      </p>
      <p className="mt-0 mb-3.5">
        This is the Home page.  From here you can manage your pension plans, your state pension(s) and any other incomes (such as interest payments, property rent, etc).
      </p>
      <div className="flex justify-end">
        <Button label="Get Started" icon="pi pi-check" onClick={onHide} autoFocus />
      </div>
    </Dialog>
  );
}
