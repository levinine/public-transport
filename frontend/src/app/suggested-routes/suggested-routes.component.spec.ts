import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SuggestedRoutesComponent } from './suggested-routes.component';

describe('SuggestedRoutesComponent', () => {
  let component: SuggestedRoutesComponent;
  let fixture: ComponentFixture<SuggestedRoutesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SuggestedRoutesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SuggestedRoutesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
