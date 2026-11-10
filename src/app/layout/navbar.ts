import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthSession } from '../core/auth-session';
import { Logo } from '../shared/logo';
import { Icon } from '../shared/icon';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, RouterLinkActive, Logo, Icon],
  template: `
    <header class="nav">
      <a routerLink="/" class="nav__brand" aria-label="CORTE STUDIO, inicio" (click)="open.set(false)"><app-logo /></a>

      <button class="nav__toggle" type="button" [attr.aria-expanded]="open()" aria-controls="menu"
        (click)="open.set(!open())" [attr.aria-label]="open() ? 'Cerrar menú' : 'Abrir menú'">
        <app-icon [name]="open() ? 'x' : 'menu'" [size]="24" />
      </button>

      <nav id="menu" class="nav__links" [class.is-open]="open()" aria-label="Principal">
        <a routerLink="/" fragment="inicio" (click)="open.set(false)">Inicio</a>
        <a routerLink="/" fragment="servicios" (click)="open.set(false)">Servicios</a>
        <a routerLink="/" fragment="barberos" (click)="open.set(false)">Barberos</a>
        <a routerLink="/" fragment="experiencia" (click)="open.set(false)">La experiencia</a>
        @if (session.user(); as user) {
          @if (user.esAdministrador) {
            <a routerLink="/admin" routerLinkActive="is-active" class="nav__login" (click)="open.set(false)">
              Panel administrador
            </a>
          } @else if (user.rol === 'BARBERO') {
            <a routerLink="/admin" routerLinkActive="is-active" class="nav__login" (click)="open.set(false)">
              Mis citas · {{ user.nombre }}
            </a>
          } @else {
            <span class="nav__user">{{ user.nombre }}</span>
          }
          <button type="button" class="nav__logout" (click)="logout()">Cerrar sesión</button>
        } @else {
          <a routerLink="/ingresar" routerLinkActive="is-active" class="nav__login" (click)="open.set(false)">
            <app-icon name="user" [size]="18" /> Ingresar
          </a>
        }
        <a routerLink="/reservar" class="btn btn--ink" (click)="open.set(false)">Reservar cita</a>
      </nav>
    </header>`
})
export class Navbar {
  readonly session = inject(AuthSession);
  private router = inject(Router);
  open = signal(false);

  logout(): void {
    this.session.clear();
    this.open.set(false);
    this.router.navigateByUrl('/');
  }
}
