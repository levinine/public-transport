import { Component, EventEmitter, OnInit, Input, Output} from '@angular/core';

@Component({
  selector: 'app-suggested-routes',
  templateUrl: './suggested-routes.component.html',
  styleUrls: ['./suggested-routes.component.scss']
})
export class SuggestedRoutesComponent implements OnInit {

  @Input() routes;
  @Output() selectRoute = new EventEmitter<number>();
  selectedIndex: number;
  constructor() { }

  ngOnInit() {
    this.selectedIndex = 0;
  }

  changeRoute(index: number) {
    this.selectedIndex = index;
    this.selectRoute.emit(index);
  }
}