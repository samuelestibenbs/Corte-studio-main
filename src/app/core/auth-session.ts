import { computed, Injectable, signal } from '@angular/core';
import { LoginResponse } from './ports/barberia-api.port';

type SessionUser = LoginResponse['usuario'];

const USER_STORAGE_KEY = 'barberia_user';

@Injectable({ providedIn: 'root' })
export class AuthSession {
  private readonly currentUser = signal<SessionUser | null>(this.restoreUser());
  readonly user = this.currentUser.asReadonly();
  readonly isAdmin = computed(() => this.currentUser()?.rol === 'ADMINISTRADOR');
  readonly isBarber = computed(() => this.currentUser()?.rol === 'BARBERO');

  save(token: string, user: SessionUser): void {
    if (typeof window !== 'undefined') {
      window.sessionStorage.setItem('barberia_token', token);
      window.sessionStorage.setItem(USER_STORAGE_KEY, JSON.stringify(user));
    }
    this.currentUser.set(user);
  }

  clear(): void {
    if (typeof window !== 'undefined') {
      window.sessionStorage.removeItem('barberia_token');
      window.sessionStorage.removeItem(USER_STORAGE_KEY);
    }
    this.currentUser.set(null);
  }

  hasToken(): boolean {
    return typeof window !== 'undefined' && !!window.sessionStorage.getItem('barberia_token');
  }

  private restoreUser(): SessionUser | null {
    if (typeof window === 'undefined') return null;
    const storedUser = window.sessionStorage.getItem(USER_STORAGE_KEY);
    if (!storedUser) return null;

    try {
      const user = JSON.parse(storedUser) as Partial<SessionUser>;
      const role = user.rol ?? (user.esAdministrador ? 'ADMINISTRADOR' : 'CLIENTE');
      if (typeof user.nombre !== 'string' || typeof user.email !== 'string'
        || typeof user.esAdministrador !== 'boolean'
        || !['ADMINISTRADOR', 'BARBERO', 'CLIENTE'].includes(role)) return null;
      return {
        nombre: user.nombre,
        email: user.email,
        esAdministrador: user.esAdministrador,
        rol: role as SessionUser['rol'],
        barbero: typeof user.barbero === 'string' ? user.barbero : null
      };
    } catch {
      window.sessionStorage.removeItem(USER_STORAGE_KEY);
      return null;
    }
  }
}