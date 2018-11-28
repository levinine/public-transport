import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';


@Injectable({
  providedIn: 'root'
})
export class SearchService {

  constructor(private http: HttpClient) { }

  getCoordsFromAddress(address: string) {

    console.log('address', address);

    // this.http.get()
    return null;
  }
}
