/**
 * Composition root: the only place that knows the concrete adapters.
 *
 * <p>It wires beans and configuration and never carries business logic. Every other layer
 * depends on the ports, not on the implementations.
 */
package br.com.consigport.config;
