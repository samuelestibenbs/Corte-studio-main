import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AdminBarber, AdminCatalog, Barber, Catalog, Service } from './catalog';
import { AdminAppointment, ApiMessage, BarberCompletedCount, BarberiaApiPort, BarberProfileUpdate, LoginResponse, NewBarber, ServiceUpdate } from './ports/barberia-api.port';
import { environment } from '../../environments/environment';

export const API_BASE_URL = environment.apiBaseUrl;

@Injectable({ providedIn: 'root' })
export class BarberiaApi implements BarberiaApiPort {
  private http = inject(HttpClient);

  getCatalog(): Observable<Catalog> {
    return this.http.get<Catalog>(`${API_BASE_URL}/catalogo`);
  }

  getAdminCatalog(): Observable<AdminCatalog> {
    return this.http.get<AdminCatalog>(`${API_BASE_URL}/admin/catalogo`, {
      headers: { Authorization: `Bearer ${sessionStorage.getItem('barberia_token') ?? ''}` }
    });
  }

  addBarber(barber: NewBarber): Observable<AdminBarber> {
    return this.http.post<AdminBarber>(`${API_BASE_URL}/admin/catalogo/barberos`, barber, {
      headers: { Authorization: `Bearer ${sessionStorage.getItem('barberia_token') ?? ''}` }
    });
  }

  deleteBarber(id: string): Observable<void> {
    return this.http.delete<void>(`${API_BASE_URL}/admin/catalogo/barberos/${encodeURIComponent(id)}`, {
      headers: { Authorization: `Bearer ${sessionStorage.getItem('barberia_token') ?? ''}` }
    });
  }

  updateBarberProfile(id: string, profile: BarberProfileUpdate): Observable<AdminBarber> {
    return this.http.put<AdminBarber>(`${API_BASE_URL}/admin/catalogo/barberos/${encodeURIComponent(id)}`, profile, {
      headers: { Authorization: `Bearer ${sessionStorage.getItem('barberia_token') ?? ''}` }
    });
  }

  updateService(id: string, service: ServiceUpdate): Observable<Service> {
    return this.http.put<Service>(`${API_BASE_URL}/admin/catalogo/servicios/${encodeURIComponent(id)}`, service, {
      headers: { Authorization: `Bearer ${sessionStorage.getItem('barberia_token') ?? ''}` }
    });
  }

  getAdminSummary(): Observable<BarberCompletedCount[]> {
    return this.http.get<BarberCompletedCount[]>(`${API_BASE_URL}/admin/resumen`, {
      headers: { Authorization: `Bearer ${sessionStorage.getItem('barberia_token') ?? ''}` }
    });
  }

  getBarberAppointments(): Observable<AdminAppointment[]> {
    return this.http.get<AdminAppointment[]>(`${API_BASE_URL}/barbero/citas`, {
      headers: { Authorization: `Bearer ${sessionStorage.getItem('barberia_token') ?? ''}` }
    });
  }

  completeBarberAppointment(id: string): Observable<AdminAppointment> {
    return this.http.patch<AdminAppointment>(`${API_BASE_URL}/barbero/citas/${encodeURIComponent(id)}/realizada`, {}, {
      headers: { Authorization: `Bearer ${sessionStorage.getItem('barberia_token') ?? ''}` }
    });
  }

  getAvailability(date: string, barber: string): Observable<{ horariosOcupados: string[] }> {
    return this.http.get<{ horariosOcupados: string[] }>(`${API_BASE_URL}/citas/disponibilidad`, {
      params: { fecha: date, barbero: barber }
    });
  }

  bookAppointment(appointment: {
    clienteNombre: string;
    barbero: string;
    servicio: string;
    fecha: string;
    hora: string;
  }): Observable<ApiMessage> {
    return this.http.post<ApiMessage>(`${API_BASE_URL}/citas/agendar`, appointment);
  }

  register(name: string, email: string, password: string): Observable<ApiMessage> {
    return this.http.post<ApiMessage>(`${API_BASE_URL}/auth/register`, { nombre: name, email, password });
  }

  login(email: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${API_BASE_URL}/auth/login`, { email, password });
  }
}
