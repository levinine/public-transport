import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';

const API_URL_SEARCH = 'https://nominatim.openstreetmap.org/search?q=';
const API_URL_REVERSE ='https://nominatim.openstreetmap.org/reverse?';
const QUERY_FORMAT = '&format=json';
const QUERY_ADDRESS_DETAILS = '&addressdetails=1';
const QUERY_LIMIT = '&limit=10';

@Injectable({
  providedIn: 'root'
})
export class SearchService {

  cityRestriction = 'novi+sad+';

  constructor(private http: HttpClient) { }

  getCoordsFromAddress(address: string) : Observable<any> {
    address = address.replace(/ /g, "+");
    let url = API_URL_SEARCH + this.cityRestriction + address + QUERY_FORMAT + QUERY_ADDRESS_DETAILS + QUERY_LIMIT;
    return this.http.get(url)
  }

  getAddressFromCoords(coords: any) {
    let lon = 'lon=' + coords[0];
    let lat = 'lat=' + coords[1];
    let url = API_URL_REVERSE + lat + '&' + lon + QUERY_FORMAT; 
    return this.http.get(url);
  }
}