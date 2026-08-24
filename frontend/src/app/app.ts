import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

/**
 * Root Angular component for the Restaurant Ordering System.
 *
 * The component provides the router outlet into which lazy-loaded customer,
 * staff and administrator pages are rendered.
 */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {}
