import { Component, HostListener, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { formatCOP } from '../../core/demo-data';
import { Barber, CATALOG_UPDATED_STORAGE_KEY, Service } from '../../core/catalog';
import { BARBERIA_API } from '../../core/ports/barberia-api.port';
import { Icon } from '../../shared/icon';
import { Photo } from '../../shared/photo';

@Component({
  selector: 'app-home',
  imports: [RouterLink, Icon, Photo],
  templateUrl: './home.html'
})
export class Home implements OnInit {
  private api = inject(BARBERIA_API);
  heroPhoto = 'https://images.unsplash.com/photo-1503951914875-452162b0f3f1?auto=format&fit=crop&w=1200&q=85';
  services = signal<Service[]>([]);
  barbers = signal<Barber[]>([]);
  catalogLoading = signal(true);
  catalogError = signal('');
  fmt = formatCOP;
  active = signal('');
  steps = [
    { n: '1', t: 'Elige tu servicio', d: 'Corte, barba o ambos. Ves cuanto dura y cuanto cuesta antes de decidir.' },
    { n: '2', t: 'Encuentra tu barbero', d: 'Conoce a tu barbero su especialidad y reserva con quien mejor encaje con tu estilo.' },
    { n: '3', t: 'Selecciona el horario', d: 'Escoge día y hora entre los espacios libres de la semana.' },
    { n: '4', t: 'Confirma tu cita', d: 'Revisa el resumen, confirma y llega a tu hora. Sin llamadas.' }
  ];

  ngOnInit() {
    this.loadCatalog();
  }

  @HostListener('window:storage', ['$event'])
  onStorage(event: StorageEvent) {
    if (event.key === CATALOG_UPDATED_STORAGE_KEY) this.loadCatalog();
  }

  loadCatalog() {
    this.catalogLoading.set(true);
    this.catalogError.set('');
    this.api.getCatalog().subscribe({
      next: catalog => {
        this.services.set(catalog.servicios);
        this.barbers.set(catalog.barberos);
        this.catalogLoading.set(false);
        if (!catalog.servicios.some(service => service.id === this.active())) {
          this.active.set(catalog.servicios[0]?.id ?? '');
        }
      },
      error: () => {
        this.catalogError.set('No se pudo cargar el catálogo. Verifica la conexión e inténtalo de nuevo.');
        this.catalogLoading.set(false);
      }
    });
  }
}
