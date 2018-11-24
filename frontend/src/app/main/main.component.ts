import { Component, OnInit } from '@angular/core';
import { RoutesService } from '../routes.service';
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

@Component({
  selector: 'app-main',
  templateUrl: './main.component.html',
  styleUrls: ['./main.component.scss']
})
export class MainComponent implements OnInit {

  // Novi Sad coordinates
  latitude: number = 45.26060794;
  longitude: number = 19.83221305;
  zoomSize: number = 16;

  routes = null;

  model = {
    'startCoords': null,
    'endCoords': null,
    'datePicker': null
  };

  showDirections = true;
  showLines = false;

  map: any;
  vectorSource = new VectorSource()

  constructor(private routesService: RoutesService) { }

  ngOnInit() {
    let vm = this
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
      if (vm.model.startCoords == null || vm.model.endCoords == null) {
        // draw marker
        vm.drawMarker(args.coordinate);
        var lonlat = transform(args.coordinate, 'EPSG:3857', 'EPSG:4326');
        if (vm.model.startCoords == null) {
          vm.model.startCoords = lonlat;
        } else if (vm.model.endCoords == null) {
          vm.model.endCoords = lonlat;
        }
      } else {
        console.log('You have already selected two markers');
      }
    });
  }

  changeTab(tabname: string): void {
    this.showDirections = tabname == 'directions' ? true : false;
    this.showLines = tabname == 'directions' ? false : true;
  }

  getRoutes() {
    // TODO: get routes from the real API
    // this service returns mocked routes
    this.routesService.getRoutes().subscribe(response => {
      this.routes = response;
      this.drawRoute(0);
    });
  }

  drawMarker(coordinates) {
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
      name: 'Marker'
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
      this.drawLine(startPoint, endPoint, activity.type);
      if (activity.type == 2) {
        // draw bus station for start and end point for each bus activities
        this.drawBusStation(activity.startCoord.lon, activity.startCoord.lat, activity.startingStation);
        this.drawBusStation(activity.endCoord.lon, activity.endCoord.lat, activity.endingStation);
      }
      console.log("Date: " + this.model.datePicker);
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
    vm.vectorSource.getFeatures().forEach(function (feature) {
      let properties = feature.getProperties();
      if (properties.name == 'Line' || properties.name == 'Station') {
        vm.vectorSource.removeFeature(feature);
      }
    });
    // draw new route
    this.drawRoute(routeIndex);
  }

  clearMap() {
    // remove features from layer
    this.vectorSource.clear();
    this.routes = null;
    this.model.startCoords = null;
    this.model.endCoords = null;
  }
}