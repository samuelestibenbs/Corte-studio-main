import { Component, HostListener, OnInit, computed, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AdminAppointment, BARBERIA_API, BarberCompletedCount } from '../../core/ports/barberia-api.port';
import { AdminBarber, AdminCatalog, CATALOG_UPDATED_STORAGE_KEY, Service } from '../../core/catalog';
import { formatCOP } from '../../core/demo-data';
import { AuthSession } from '../../core/auth-session';

type SaveFeedback = { state: 'saving' | 'saved' | 'error'; message: string };

@Component({
  selector: 'app-admin',
  imports: [FormsModule],
  templateUrl: './admin.html'
})
export class Admin implements OnInit {
  private api = inject(BARBERIA_API);
  private router = inject(Router);
  private session = inject(AuthSession);

  appointments = signal<AdminAppointment[]>([]);
  summary = signal<BarberCompletedCount[]>([]);
  catalog = signal<AdminCatalog>({ barberos: [], servicios: [] });
  loading = signal(false);
  catalogLoading = signal(false);
  error = signal('');
  updatingId = signal<string | null>(null);
  savingBarberId = signal<string | null>(null);
  savingServiceId = signal<string | null>(null);
  addingBarber = signal(false);
  deletingBarberId = signal<string | null>(null);
  barberFeedback = signal<Record<string, SaveFeedback>>({});
  serviceFeedback = signal<Record<string, SaveFeedback>>({});
  catalogActionFeedback = signal<SaveFeedback | null>(null);
  newBarber = {
    name: '',
    email: '',
    specialty: '',
    bio: '',
    photoLabel: '',
    photoSrc: ''
  };
  fmt = formatCOP;
  isBarber = computed(() => this.session.isBarber());

  ngOnInit() {
    this.loadAppointments();
  }

  @HostListener('window:storage', ['$event'])
  onStorage(event: StorageEvent) {
    if (event.key === CATALOG_UPDATED_STORAGE_KEY && !this.isBarber()) this.loadAppointments();
  }

  loadAppointments() {
    this.loading.set(true);
    this.error.set('');
    if (this.isBarber()) {
      this.api.getBarberAppointments().subscribe({
        next: appointments => {
          this.appointments.set(appointments);
          this.loading.set(false);
        },
        error: error => this.handleError(error)
      });
      return;
    }
    this.api.getAdminSummary().subscribe({
      next: summary => {
        this.summary.set(summary);
        this.loading.set(false);
        this.loadCatalog();
      },
      error: error => this.handleError(error)
    });
  }

  loadCatalog() {
    this.catalogLoading.set(true);
    this.api.getAdminCatalog().subscribe({
      next: catalog => {
        this.catalog.set(catalog);
        this.catalogLoading.set(false);
      },
      error: error => {
        this.handleError(error);
        this.catalogLoading.set(false);
      }
    });
  }

  saveBarber(barber: AdminBarber) {
    this.savingBarberId.set(barber.id);
    this.barberFeedback.update(feedback => ({
      ...feedback,
      [barber.id]: { state: 'saving', message: 'Guardando perfil…' }
    }));
    this.error.set('');
    this.api.updateBarberProfile(barber.id, {
      specialty: barber.specialty,
      bio: barber.bio,
      photoLabel: barber.photoLabel,
      photoSrc: barber.photoSrc,
      disponible: barber.disponible
    }).subscribe({
      next: updated => {
        this.catalog.update(value => ({
          ...value,
          barberos: value.barberos.map(item => item.id === updated.id ? updated : item)
        }));
        this.barberFeedback.update(feedback => ({
          ...feedback,
          [barber.id]: { state: 'saved', message: `Perfil de ${updated.name} guardado.` }
        }));
        this.publishCatalogChange();
        this.savingBarberId.set(null);
      },
      error: error => {
        this.barberFeedback.update(feedback => ({
          ...feedback,
          [barber.id]: { state: 'error', message: this.getSaveError(error, 'No se guardó el perfil') }
        }));
        this.handleError(error);
        this.savingBarberId.set(null);
      }
    });
  }

  saveService(service: Service) {
    this.savingServiceId.set(service.id);
    this.serviceFeedback.update(feedback => ({
      ...feedback,
      [service.id]: { state: 'saving', message: 'Guardando precio…' }
    }));
    this.error.set('');
    this.api.updateService(service.id, { description: service.description, price: service.price }).subscribe({
      next: updated => {
        this.catalog.update(value => ({
          ...value,
          servicios: value.servicios.map(item => item.id === updated.id ? updated : item)
        }));
        this.serviceFeedback.update(feedback => ({
          ...feedback,
          [service.id]: { state: 'saved', message: `Precio de ${updated.name} guardado: ${this.fmt(updated.price)}.` }
        }));
        this.publishCatalogChange();
        this.savingServiceId.set(null);
      },
      error: error => {
        this.serviceFeedback.update(feedback => ({
          ...feedback,
          [service.id]: { state: 'error', message: this.getSaveError(error, 'No se guardó el precio') }
        }));
        this.handleError(error);
        this.savingServiceId.set(null);
      }
    });
  }

  addBarber() {
    this.addingBarber.set(true);
    this.catalogActionFeedback.set({ state: 'saving', message: 'Agregando barbero…' });
    this.error.set('');
    this.api.addBarber(this.newBarber).subscribe({
      next: created => {
        this.catalog.update(value => ({ ...value, barberos: [...value.barberos, created] }));
        this.newBarber = { name: '', email: '', specialty: '', bio: '', photoLabel: '', photoSrc: '' };
        this.catalogActionFeedback.set({ state: 'saved', message: `${created.name} fue agregado. Registra una cuenta con ${created.email} para habilitar su acceso.` });
        this.addingBarber.set(false);
        this.publishCatalogChange();
        this.loadAppointments();
      },
      error: error => {
        this.catalogActionFeedback.set({ state: 'error', message: this.getSaveError(error, 'No se pudo agregar el barbero') });
        this.handleError(error);
        this.addingBarber.set(false);
      }
    });
  }

  deleteBarber(barber: AdminBarber) {
    if (!window.confirm(`¿Eliminar a ${barber.name} del catálogo? Sus citas históricas se conservarán.`)) return;
    this.deletingBarberId.set(barber.id);
    this.catalogActionFeedback.set({ state: 'saving', message: `Eliminando a ${barber.name}…` });
    this.error.set('');
    this.api.deleteBarber(barber.id).subscribe({
      next: () => {
        this.catalog.update(value => ({
          ...value,
          barberos: value.barberos.filter(item => item.id !== barber.id)
        }));
        this.catalogActionFeedback.set({ state: 'saved', message: `${barber.name} fue eliminado del catálogo.` });
        this.deletingBarberId.set(null);
        this.publishCatalogChange();
        this.loadAppointments();
      },
      error: error => {
        this.catalogActionFeedback.set({ state: 'error', message: this.getSaveError(error, 'No se pudo eliminar el barbero') });
        this.handleError(error);
        this.deletingBarberId.set(null);
      }
    });
  }

  markCompleted(appointment: AdminAppointment) {
    if (appointment.estado === 'realizada') return;
    this.updatingId.set(appointment._id);
    this.error.set('');
    this.api.completeBarberAppointment(appointment._id).subscribe({
      next: updated => {
        this.appointments.update(appointments =>
          appointments.map(item => item._id === updated._id ? updated : item)
        );
        this.updatingId.set(null);
      },
      error: error => {
        this.handleError(error);
        this.updatingId.set(null);
      }
    });
  }

  formatDate(date: string): string {
    return new Intl.DateTimeFormat('es-CO', {
      weekday: 'short',
      day: 'numeric',
      month: 'short',
      year: 'numeric'
    }).format(new Date(`${date}T00:00:00`));
  }

  logout() {
    this.session.clear();
    this.router.navigateByUrl('/ingresar');
  }

  private handleError(error: HttpErrorResponse) {
    this.error.set(error.error?.error ?? 'No se pudieron cargar o actualizar las citas. Inténtalo de nuevo.');
    this.loading.set(false);
  }

  private getSaveError(error: HttpErrorResponse, fallback: string): string {
    if (error.status === 0) return 'Sin conexión con la API. Comprueba que esté activa.';
    return error.error?.error ?? `${fallback} (HTTP ${error.status}).`;
  }

  private publishCatalogChange() {
    if (typeof window !== 'undefined') {
      window.localStorage.setItem(CATALOG_UPDATED_STORAGE_KEY, String(Date.now()));
    }
  }
}
