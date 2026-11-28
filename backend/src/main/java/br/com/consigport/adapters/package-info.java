/**
 * Inbound and outbound adapters.
 *
 * <p>Inbound adapters translate HTTP into use cases; outbound adapters implement the ports
 * declared by the domain and by the application. An inbound adapter never reaches into an
 * outbound one: both talk to the use cases and to the ports.
 */
package br.com.consigport.adapters;
