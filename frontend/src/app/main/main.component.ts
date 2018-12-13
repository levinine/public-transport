import { Component, OnInit, HostListener } from '@angular/core';
import { NgxSpinnerService } from 'ngx-spinner';

import { RoutesService } from '../routes.service';
import { SearchService } from '../search.service';
import { TranslateService } from '@ngx-translate/core';
import { LayoutService } from '../layout.service';

import Map from 'ol/Map';
import View from 'ol/View';
import TileLayer from 'ol/layer/Tile';
import VectorLayer from 'ol/layer/Vector';
import VectorSource from 'ol/source/Vector';
import OSM from 'ol/source/OSM';
import {fromLonLat} from 'ol/proj';
import {transform} from 'ol/proj';
import Style from 'ol/style/Style';
import Icon from 'ol/style/Icon';
import Feature from 'ol/Feature';
import Point from 'ol/geom/Point';
import Stroke from 'ol/style/Stroke';
import LineString from 'ol/geom/LineString';
import Text from 'ol/style/Text';
import Fill from 'ol/style/Fill';

const START_DESTINATION = 'startDestination';
const END_DESTINATION = 'endDestination';
const MOBILE_DEVICE = 'mobile';
const DESKTOP_DEVICE = 'device';

@Component({
  selector: 'app-main',
  templateUrl: './main.component.html',
  styleUrls: ['./main.component.scss']
})
export class MainComponent implements OnInit {

  showSidebar = true;
  sidebarState: string;

  // Novi Sad coordinates
  latitude: number = 45.26060794;
  longitude: number = 19.83221305;
  zoomSize: number = 13;

  routes = null;
  selectedRoute: number;
  startDestinationAddresses = [];
  endDestinationAddresses = [];

  model = {
    'dateTime': new Date(),
    'startDestination': {
      'info' : null,
      'lon': null,
      'lat': null
    },
    'endDestination': {
      'info' : null,
      'lon': null,
      'lat': null
    }
  };
  datePickerStartAt = new Date();
  datePickerMinValue = new Date();

  showDirections = true;
  showLines = false;

  map: any;
  vectorSource = new VectorSource()

  constructor(private routesService: RoutesService, private searchService: SearchService, private translate: TranslateService, private layoutService: LayoutService, private spinner: NgxSpinnerService) { }

  ngOnInit() {
    let vm = this;
    this.chooseStateForSidebar();

    this.layoutService.toggleSidebarEmmiter.subscribe(result => {
      this.showSidebar = this.sidebarState == MOBILE_DEVICE ? result : null;
    })

    // create map and set initial layers
    vm.map = new Map({
      target: 'map',
      layers: [
        new TileLayer({
          source: new OSM()
        }),
        new VectorLayer({
          source: this.vectorSource
        })
      ],
      view: new View({
        center: fromLonLat([this.longitude, this.latitude]),
        zoom: this.zoomSize
      })
    });
    // set marker on click event
    vm.map.on('click', function (args) {
      if ((vm.model.startDestination.info == null || vm.model.endDestination.info == null) && vm.showDirections) {
        // draw marker
        var lonlat = transform(args.coordinate, 'EPSG:3857', 'EPSG:4326');
        if (vm.model.startDestination.info == null) {
          vm.drawMarker(args.coordinate, START_DESTINATION);
          vm.searchService.getAddressFromCoords(lonlat).subscribe(result => {
            vm.setModel(vm.model.startDestination, result);
          });
        } else if (vm.model.endDestination.info == null) {
          vm.drawMarker(args.coordinate, END_DESTINATION);
          vm.searchService.getAddressFromCoords(lonlat).subscribe(result => {
            vm.setModel(vm.model.endDestination, result);
            // open sidebar if mobile view is active
            if(vm.sidebarState == MOBILE_DEVICE) {
              vm.layoutService.toggleSidebar();
            }
          });
        }
      } else if(vm.showLines) {
        console.log('Selection is not allowed in lines mode');
      }
       else {
        console.log('You have already selected two markers');
      }
    });
  }

  chooseStateForSidebar() {
    this.sidebarState = window.innerWidth < 991 ? MOBILE_DEVICE : DESKTOP_DEVICE;
    this.showSidebar = this.sidebarState == MOBILE_DEVICE ? this.showSidebar : null;
  }

  @HostListener('window:resize', ['$event'])
  onResize(event) {
    this.chooseStateForSidebar();
  }
  
  changeTab(tabname: string): void {
    this.showDirections = tabname == 'directions' ? true : false;
    this.showLines = tabname == 'directions' ? false : true;
    this.clearMap();
    this.clearRoutes();
    this.clearModel(this.model.startDestination);
    this.clearModel(this.model.endDestination);
  }

  getRoutes() {
    // get optimal routes from backend
    this.spinner.show();
    this.routesService.getRoutes(this.model).subscribe(
      data => {
        this.routes = data.routes;
        this.clearLinesAndStations();
        this.drawRoute(0);
        this.selectedRoute = 0;
        this.layoutService.toggleSidebar();
        this.spinner.hide();
      },
      error => {
        console.log(error)
      },
    );
  }

  // call external api to get list of addresses which contains searched characters
  onDestinationKeyUp(evt: any, model: any, addressesList: any) {
    this.searchService.getCoordsFromAddress(model.info).subscribe(result => {
      this[addressesList] = result;
    });
  }
  // set chosen address to it's model and draw marker
  setDestinationModel(address: any, model: any, addressesList: any) {
    this.setModel(model, address);
    this[addressesList] = [];
    this.drawMarker(fromLonLat([+model.lon, +model.lat]), this.model.startDestination === model ? START_DESTINATION : END_DESTINATION);
  }

  clearModel(model: any) {
    model.lon = null;
    model.lat = null;
    model.info = null;
  }

  setModel(model: any, data: any) {
    model.lon = data['lon'];
    model.lat = data['lat'];
    model.info = data['display_name'];
  }

  clearPoint(model: any, pointName: string) {
    this.clearModel(model);
    this.clearMarker(pointName);
    this.clearLinesAndStations();
    this.clearRoutes();
    this.clearSearchedAddresses(pointName);
    this.model.dateTime = new Date();
  }

  clearLinesAndStations() {
    for(let feature of this.vectorSource.getFeatures()) {
      let properties = feature.getProperties();
      if (properties.name == "Line" || properties.name == "Station") {
        this.vectorSource.removeFeature(feature);
      }
    };
  }

  clearAllContent() {
    this.clearMap();
    this.clearRoutes();
    this.clearModel(this.model.startDestination);
    this.clearModel(this.model.endDestination);
    this.model.dateTime = new Date();
  }

  clearMarker(markerName: string) {
    for(let feature of this.vectorSource.getFeatures()) {
      let properties = feature.getProperties();
      if (properties.name == markerName) {
        this.vectorSource.removeFeature(feature);
        break;
      }
    };
  }

  clearRoutes() {
    this.routes = null;
  }

  clearMap() {
    // remove features from layer
    this.vectorSource.clear();
  }

  clearSearchedAddresses(name: string) {
    this[name+'Addresses'] = [];
  }

  setGeolocation() {
    let vm = this;
    // remove marker for start coords, clear lines and stations, clear routes
    this.clearMarker(START_DESTINATION);
    this.clearLinesAndStations();
    this.clearRoutes();
    // get user location
    navigator.geolocation.getCurrentPosition(result => {
      const lon = result.coords.longitude;
      const lat = result.coords.latitude;
      // set start coords and draw marker
      this.searchService.getAddressFromCoords([lon, lat]).subscribe(address => {
        this.setModel(this.model.startDestination, address);
        vm.drawMarker(fromLonLat([lon, lat]), START_DESTINATION);
        this.startDestinationAddresses = [];
      });
    });
  }

  drawMarker(coordinates, direction) {
    // define style for the marker
    let markerStyle = new Style({
      image: new Icon({
        anchor: [0.5, 1],
        anchorXUnits: 'fraction',
        anchorYUnits: 'fraction',
        opacity: 1,
        src: '../../assets/images/map-marker.png',
        scale: 0.1
      }),
      zIndex: 5
    });
    // create marker feature and set style
    let marker = new Feature({
      geometry: new Point(coordinates),
      name: direction == START_DESTINATION ? START_DESTINATION : END_DESTINATION
    });
    marker.setStyle(markerStyle);
    // add marker to layer
    this.vectorSource.addFeature(marker);
  }

  drawRoute(routeIndex: number) {
    for (let activity of this.routes[routeIndex ? routeIndex : 0].activities) {
      let startPoint = fromLonLat([activity.startCoord.lon, activity.startCoord.lat]);
      let endPoint = fromLonLat([activity.endCoord.lon, activity.endCoord.lat]);
      // draw activity line
      if((activity.type == 1 && !activity.pathCoordinates) || (activity.type == 2 && !activity.pathCoordinates)) {
        this.drawLine(startPoint, endPoint, activity.type);
      }
      if (activity.type == 2) {
        // draw bus station for start and end point for each bus activities
        this.drawBusStation(activity.startCoord.lon, activity.startCoord.lat, activity.startingStation);
        this.drawBusStation(activity.endCoord.lon, activity.endCoord.lat, activity.endingStation);
        // draw bus path if exists
        if(activity.pathCoordinates) {
          for(let i=0; i<activity.pathCoordinates.length-1; i++) {
            this.drawLine(fromLonLat([activity.pathCoordinates[i].lon, activity.pathCoordinates[i].lat]), fromLonLat([activity.pathCoordinates[i+1].lon, activity.pathCoordinates[i+1].lat]), activity.type);
          }
        }
      }
    }
  }

  drawLine(startPoint, endPoint, type) {
    // define styles for walking and bus road
    let busRoadLineStyle = [
      new Style({
        stroke: new Stroke({
          color: '#1434A0',
          width: 4
        })
      })
    ];
    let walkingRoadLineStyle = [
      new Style({
        stroke: new Stroke({
          color: '#ff0000',
          width: 4,
          lineDash: [10]
        })
      })
    ];
    // create line feature and set appropriate style
    let line = new Feature({
      geometry: new LineString([startPoint, endPoint]),
      name: 'Line',
    });
    // activities: type 1 - walking, type 2 - bus 
    line.setStyle(type == 1 ? walkingRoadLineStyle : busRoadLineStyle);
    // add line to the layer
    this.vectorSource.addFeature(line);
  }

  drawBusStation(lon, lat, stationName) {
    // create station feature and set style
    let busStationStyle = new Style({
      image: new Icon({
        anchor: [0.5, 1],
        anchorXUnits: 'fraction',
        anchorYUnits: 'fraction',
        opacity: 1,
        src: '../../assets/images/bus-station.png',
        scale: 0.07
      }),
      text: new Text({
        text: stationName,
        stroke: new Stroke({
          color: '#fff' 
        }),
        fill: new Fill({
          color: '#3366cc'
        }),
        font: '15px sans-serif',
        offsetY: -45,
        backgroundFill: new Fill({
          color: 'white'
        })
      })
    });
    let busStation = new Feature({
      geometry: new Point(fromLonLat([lon, lat])),
      name: 'Station'
    });
    busStation.setStyle(busStationStyle);
    // add bus station to the layer
    this.vectorSource.addFeature(busStation);
  }

  onRouteSelection(routeIndex: number) {
    // replace the old route with the newly selected
    let vm = this;
    // remove lines and bus stations from the old one
    vm.clearLinesAndStations();
    // draw new route
    this.drawRoute(routeIndex);
    this.selectedRoute = routeIndex;
    if (this.showSidebar == true) {
      this.layoutService.toggleSidebar();
    }
  }

  onBusLineSelection(line: any) {
    this.clearMap();
    for(let i=0; i<line.coordinates.length-1; i++) {
      this.drawLine(fromLonLat([line.coordinates[i].lon, line.coordinates[i].lat]), fromLonLat([line.coordinates[i+1].lon, line.coordinates[i+1].lat]), 2);
    }
    if(line.stops) {
      for(let i=0; i<line.stops.length; i++) {
        this.drawBusStation(line.stops[i].lon, line.stops[i].lat, '');
      }
    }
    this.layoutService.toggleSidebar();
  }

  useLanguage(language: string) {
    this.translate.use(language);
  }
}