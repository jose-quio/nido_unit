import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ApartamentoComponent } from './apartamento.component';

describe('ApartamentoComponent', () => {
  let component: ApartamentoComponent;
  let fixture: ComponentFixture<ApartamentoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ApartamentoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ApartamentoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
