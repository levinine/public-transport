import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';

const API_URL = '/api/v1';

@Injectable({
  providedIn: 'root'
})
export class LinesService {

  constructor(private http: HttpClient) { }

  getLines(): Observable<any>{
    return this.http.get(`${API_URL}/lines`);
  }
}
