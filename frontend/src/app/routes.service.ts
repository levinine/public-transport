import { Injectable } from '@angular/core';
import { ROUTES } from '../app/mock-routes';
import { Observable, of } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class RoutesService {

  constructor() { }

  getRoutes():  Observable<any> {
    return of (ROUTES.routes);
  }
}
