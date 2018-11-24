import { Injectable } from '@angular/core';
import { ROUTES } from '../app/mock-routes';
import { Observable, of } from 'rxjs';
import { HttpClient } from '@angular/common/http';

const API_URL = 'http://localhost:9090/api/v1';

@Injectable({
  providedIn: 'root'
})
export class RoutesService {

  constructor(private http: HttpClient) { }

  getRoutesMock():  Observable<any> {
    return of (ROUTES.routes);
  }

  getRoutes(start: String, end: String, date: String):  Observable<any> {
      let headers = new Headers();
      headers.append('Content-Type', 'application/json');
      // @ts-ignore
      return this.http.get(`${API_URL}/routes?start=${start}&end=${end}&date=${date}`, headers);
  }
}
