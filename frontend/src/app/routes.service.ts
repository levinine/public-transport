import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';

const API_URL = '/api/v1';
// const API_URL = 'http://localhost:8080/api/v1';

@Injectable({
  providedIn: 'root'
})
export class RoutesService {

  constructor(private http: HttpClient) { }

  getRoutes(model: any):  Observable<any> {
      let startDestination = model.startDestination.lon+","+model.startDestination.lat;
      let endDestination = model.endDestination.lon+","+model.endDestination.lat
      let date = model.dateTime.toISOString();
      return this.http.get(`${API_URL}/routes?start=${startDestination}&end=${endDestination}&date=${date}`);
  }
}