import { InjectionToken } from '@angular/core';
import { Observable } from 'rxjs';
import { AdminBarber, AdminCatalog, Barber, Catalog, Service } from '../catalog';

export interface ApiMessage {
  mensaje: string;
  notificacionCalendar?: { enviada: boolean; mensaje: string };
}

export interface LoginResponse {
  token: string;
  usuario: {
    nombre: string;
    email: string;
    esAdministrador: boolean;
    rol: 'ADMINISTRADOR' | 'BARBERO' | 'CLIENTE';
    barbero: string | null;
  };
}

export interface BarberCompletedCount {
  barbero: string;
  realizadas: number;
}

export type BarberProfileUpdate = Pick<AdminBarber, 'specialty' | 'bio' | 'photoLabel' | 'photoSrc' | 'disponible'>;
export type NewBarber = Pick<AdminBarber, 'name' | 'email' | 'specialty' | 'bio' | 'photoLabel' | 'photoSrc'>;
export type ServiceUpdate = Pick<Service, 'description' | 'price'>;

export interface AdminAppointment {
  _id: string;
  clienteNombre: string;
  barbero: string;
  servicio: string;
  fecha: string;
  hora: string;
  estado?: 'reservada' | 'realizada';
}

export interface BarberiaApiPort {
  getCatalog(): Observable<Catalog>;
  getAdminCatalog(): Observable<AdminCatalog>;
  addBarber(barber: NewBarber): Observable<AdminBarber>;
  deleteBarber(id: string): Observable<void>;
  updateBarberProfile(id: string, profile: BarberProfileUpdate): Observable<AdminBarber>;
  updateService(id: string, service: ServiceUpdate): Observable<Service>;
  getAdminSummary(): Observable<BarberCompletedCount[]>;
  getBarberAppointments(): Observable<AdminAppointment[]>;
  completeBarberAppointment(id: string): Observable<AdminAppointment>;
  getAvailability(date: string, barber: string): Observable<{ horariosOcupados: string[] }>;
  bookAppointment(appointment: {
    clienteNombre: string;
    barbero: string;
    servicio: string;
    fecha: string;
    hora: string;
  }): Observable<ApiMessage>;
  register(name: string, email: string, password: string): Observable<ApiMessage>;
  login(email: string, password: string): Observable<LoginResponse>;
}

export const BARBERIA_API = new InjectionToken<BarberiaApiPort>('BARBERIA_API');
