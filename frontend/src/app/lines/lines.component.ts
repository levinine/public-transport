import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { LinesService } from '../lines.service';

@Component({
  selector: 'app-lines',
  templateUrl: './lines.component.html',
  styleUrls: ['./lines.component.scss']
})
export class LinesComponent implements OnInit {

  @Output() selectLine = new EventEmitter<number>();

  lines: any;

  constructor(private linesService: LinesService) { }
  
  ngOnInit() {
    this.getLines();
  }

  getLines() {
    this.linesService.getLines().subscribe(result => {
      this.lines = result;
    },
    error => {
        console.log(error);
    });
  }

  changeLine(line) {
    this.selectLine.emit(line);
  }
}
