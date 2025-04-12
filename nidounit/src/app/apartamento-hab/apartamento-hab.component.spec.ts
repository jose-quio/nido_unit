import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ApartamentoHabComponent } from './apartamento-hab.component';

describe('ApartamentoHabComponent', () => {
  let component: ApartamentoHabComponent;
  let fixture: ComponentFixture<ApartamentoHabComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ApartamentoHabComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ApartamentoHabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
