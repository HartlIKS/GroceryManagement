import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavigationComponent } from './components/navigation/navigation.component';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, NavigationComponent, CommonModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {

}
