import { TestBed } from '@angular/core/testing';

import { LinesService } from './lines.service';

describe('LinesService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: LinesService = TestBed.get(LinesService);
    expect(service).toBeTruthy();
  });
});
