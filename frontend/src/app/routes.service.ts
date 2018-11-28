import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';

const API_URL = 'http://localhost:8080/api/v1'; // izbrisi localhost kad pravis jar

@Injectable({
  providedIn: 'root'
})
export class RoutesService {

  constructor(private http: HttpClient) { }

  getRoutes(start: String, end: String, date: String):  Observable<any> {
      let headers = new Headers();
      headers.append('Content-Type', 'application/json');
      // @ts-ignore
      let reverseStart;
      let reverseEnd;

      // @ts-ignore
      return this.http.get(`${API_URL}/routes?start=${start}&end=${end}&date=${date}`, headers);
  }
}
