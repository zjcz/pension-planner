import { useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from 'primereact/button';
import { Menu } from 'primereact/menu';
import type { MenuItem } from 'primereact/menuitem';
import { useAuth } from '../auth/AuthContext';
import { ExportDialog } from './ExportDialog';

interface UserMenuProps {
  onOpenSettings?: () => void;
  showSettings?: boolean;
  onOpenExport?: () => void;
}

export function UserMenu({ onOpenSettings, showSettings = false, onOpenExport }: UserMenuProps) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const menuRef = useRef<Menu>(null);
  const [exportVisible, setExportVisible] = useState(false);

  const onLogout = async () => {
    await logout();
    navigate('/login', { replace: true });
  };

  const items: MenuItem[] = [
    {
      label: `Signed in as ${user?.username}`,
      disabled: true,
    },
    {
      separator: true,
    },
  ];

  if (showSettings && onOpenSettings) {
    items.push({
      label: 'Settings',
      icon: 'pi pi-cog',
      command: () => onOpenSettings(),
    });
  }

  items.push({
    label: 'Export',
    icon: 'pi pi-download',
    command: () => {
      if (onOpenExport) {
        onOpenExport();
      } else {
        setExportVisible(true);
      }
    },
  });

  items.push({
    label: 'Log Out',
    icon: 'pi pi-sign-out',
    command: onLogout,
  });

  return (
    <>
      <Menu model={items} popup ref={menuRef} />
      <Button
        icon="pi pi-user"
        rounded
        text
        aria-label="User menu"
        onClick={(event) => menuRef.current?.toggle(event)}
      />
      <ExportDialog visible={exportVisible} onHide={() => setExportVisible(false)} />
    </>
  );
}
