import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';

const API_URL = 'http://localhost:8080/api/v1'; // izbrisi localhost kad pravis jar

@Injectable({
  providedIn: 'root'
})
export class LinesService {

  constructor(private http: HttpClient) { }

  getLines() :  Observable<any>{
    let headers = new Headers();
    headers.append('Content-Type', 'application/json');
    // @ts-ignore
    return this.http.get(`${API_URL}/lines`, headers);
  }
}
