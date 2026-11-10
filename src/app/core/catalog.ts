export interface Service {
  id: string;
  name: string;
  description: string;
  minutes: number;
  price: number;
}

export interface Barber {
  id: string;
  name: string;
  specialty: string;
  bio: string;
  photoLabel: string;
  photoSrc: string;
}

export interface AdminBarber extends Barber {
  email: string;
  disponible: boolean;
}

export interface Catalog {
  barberos: Barber[];
  servicios: Service[];
}

export interface AdminCatalog {
  barberos: AdminBarber[];
  servicios: Service[];
}

export const CATALOG_UPDATED_STORAGE_KEY = 'corte_studio_catalog_updated';