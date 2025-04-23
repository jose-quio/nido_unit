import { TestBed } from '@angular/core/testing';

import { BackserviceService } from './backservice.service';

describe('BackserviceService', () => {
  let service: BackserviceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(BackserviceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
