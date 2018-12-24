import { Injectable, Output, EventEmitter } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class LayoutService {
  sidebarVisible: boolean = true;
  @Output() toggleSidebarEmmiter : EventEmitter<boolean> = new EventEmitter();

  constructor() { }

  toggleSidebar() {
    this.sidebarVisible = !this.sidebarVisible;
    this.toggleSidebarEmmiter.emit(this.sidebarVisible);
  }

  getSidebarStatus() {
    return this.sidebarVisible;
  }
}
