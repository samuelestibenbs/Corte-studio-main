import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthSession } from './auth-session';

export const adminGuard: CanActivateFn = () => {
  const session = inject(AuthSession);
  if (session.hasToken() && (session.isAdmin() || session.isBarber())) return true;
  return inject(Router).parseUrl(session.user() ? '/' : '/ingresar');
};
