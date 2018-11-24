import { Injectable } from '@angular/core';
import { ROUTES } from '../app/mock-routes';
import { Observable, of } from 'rxjs';
import { HttpClient } from '@angular/common/http';

const API_URL = 'localhost:9090/api/v1';

@Injectable({
  providedIn: 'root'
})
export class RoutesService {

  constructor(private http: HttpClient) { }

  getRoutes(start: String, end: String, date: String):  Observable<any> {
    return this.http.get(`${API_URL}/routes?start=${start}&end=${end}&date=${date}`);
  }
}
