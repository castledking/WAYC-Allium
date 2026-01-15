// Folia Tracker Web Dashboard

class FoliaTrackerApp {
    constructor() {
        this.currentPage = 0;
        this.pageSize = 50;
        this.totalPages = 1;
        this.filters = {
            player: '',
            method: ''
        };
        this.init();
    }

    async init() {
        await this.checkAuthStatus();
        this.bindEvents();
    }

    async checkAuthStatus() {
        try {
            const response = await fetch('/api/auth/status');
            const data = await response.json();

            if (data.needsSetup) {
                this.showSetupForm();
            } else if (data.authenticated) {
                this.showDashboard();
                this.loadData();
            } else {
                this.showLoginForm();
            }
        } catch (error) {
            console.error('Auth check failed:', error);
            this.showLoginForm();
        }
    }

    showSetupForm() {
        document.getElementById('login-screen').classList.remove('hidden');
        document.getElementById('dashboard-screen').classList.add('hidden');
        document.getElementById('setup-form').classList.remove('hidden');
        document.getElementById('login-form').classList.add('hidden');
    }

    showLoginForm() {
        document.getElementById('login-screen').classList.remove('hidden');
        document.getElementById('dashboard-screen').classList.add('hidden');
        document.getElementById('setup-form').classList.add('hidden');
        document.getElementById('login-form').classList.remove('hidden');
    }

    showDashboard() {
        document.getElementById('login-screen').classList.add('hidden');
        document.getElementById('dashboard-screen').classList.remove('hidden');
        document.getElementById('dashboard-screen').classList.add('flex');
    }

    bindEvents() {
        // Setup form
        document.getElementById('setup-btn').addEventListener('click', () => this.handleSetup());
        document.getElementById('setup-password-confirm').addEventListener('keypress', (e) => {
            if (e.key === 'Enter') this.handleSetup();
        });

        // Login form
        document.getElementById('login-btn').addEventListener('click', () => this.handleLogin());
        document.getElementById('login-password').addEventListener('keypress', (e) => {
            if (e.key === 'Enter') this.handleLogin();
        });

        // Navigation
        document.getElementById('logout-btn').addEventListener('click', () => this.handleLogout());
        document.getElementById('refresh-btn').addEventListener('click', () => this.loadData());

        // Filters
        document.getElementById('apply-filter-btn').addEventListener('click', () => this.applyFilters());
        document.getElementById('clear-filter-btn').addEventListener('click', () => this.clearFilters());

        // Pagination
        document.getElementById('prev-page').addEventListener('click', () => this.prevPage());
        document.getElementById('next-page').addEventListener('click', () => this.nextPage());

        // Enter key for filters
        document.getElementById('filter-player').addEventListener('keypress', (e) => {
            if (e.key === 'Enter') this.applyFilters();
        });

        // Clear all logs
        document.getElementById('clear-logs-btn').addEventListener('click', () => this.handleClearLogs());
    }

    async handleSetup() {
        const password = document.getElementById('setup-password').value;
        const confirm = document.getElementById('setup-password-confirm').value;
        const errorEl = document.getElementById('setup-error');

        errorEl.classList.add('hidden');

        if (password.length < 4) {
            errorEl.textContent = 'Password must be at least 4 characters';
            errorEl.classList.remove('hidden');
            return;
        }

        if (password !== confirm) {
            errorEl.textContent = 'Passwords do not match';
            errorEl.classList.remove('hidden');
            return;
        }

        try {
            const response = await fetch('/api/setup', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ password })
            });

            const data = await response.json();

            if (data.success) {
                this.showDashboard();
                this.loadData();
            } else {
                errorEl.textContent = data.message;
                errorEl.classList.remove('hidden');
            }
        } catch (error) {
            errorEl.textContent = 'Connection error. Please try again.';
            errorEl.classList.remove('hidden');
        }
    }

    async handleLogin() {
        const password = document.getElementById('login-password').value;
        const errorEl = document.getElementById('login-error');

        errorEl.classList.add('hidden');

        try {
            const response = await fetch('/api/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ password })
            });

            const data = await response.json();

            if (data.success) {
                this.showDashboard();
                this.loadData();
            } else {
                errorEl.textContent = data.message || 'Invalid password';
                errorEl.classList.remove('hidden');
            }
        } catch (error) {
            errorEl.textContent = 'Connection error. Please try again.';
            errorEl.classList.remove('hidden');
        }
    }

    async handleLogout() {
        try {
            await fetch('/api/logout', { method: 'POST' });
        } catch (error) {
            console.error('Logout error:', error);
        }
        this.showLoginForm();
    }

    async loadData() {
        await Promise.all([
            this.loadLogs(),
            this.loadStats()
        ]);
    }

    async loadLogs() {
        try {
            const params = new URLSearchParams({
                page: this.currentPage.toString(),
                pageSize: this.pageSize.toString()
            });

            if (this.filters.player) params.append('player', this.filters.player);
            if (this.filters.method) params.append('method', this.filters.method);

            const response = await fetch(`/api/logs?${params}`);

            if (response.status === 401) {
                this.showLoginForm();
                return;
            }

            const data = await response.json();
            this.renderLogs(data.logs);
            this.updatePagination(data);

        } catch (error) {
            console.error('Failed to load logs:', error);
        }
    }

    async loadStats() {
        try {
            const response = await fetch('/api/stats');

            if (response.status === 401) {
                this.showLoginForm();
                return;
            }

            const data = await response.json();
            this.renderStats(data);

        } catch (error) {
            console.error('Failed to load stats:', error);
        }
    }

    renderLogs(logs) {
        const container = document.getElementById('logs-body');

        if (!logs || logs.length === 0) {
            container.innerHTML = `
                <div class="flex items-center justify-center h-full text-slate-400">
                    <div class="text-center">
                        <span class="material-symbols-outlined text-4xl mb-2 block">inbox</span>
                        <p>No logs found</p>
                    </div>
                </div>
            `;
            return;
        }

        container.innerHTML = logs.map(log => this.createLogRow(log)).join('');
    }

    createLogRow(log) {
        const methodClass = this.getMethodClass(log.method);
        const methodLabel = this.getMethodLabel(log.method);
        const itemAbbr = this.getItemAbbreviation(log.itemType);
        const itemColor = this.getItemColor(log.itemType);

        return `
            <div class="grid grid-cols-12 gap-4 p-3 rounded-lg hover:bg-slate-50 dark:hover:bg-slate-800/50 transition-colors items-center border border-transparent hover:border-slate-200 dark:hover:border-slate-700">
                <div class="col-span-2 text-slate-500 dark:text-slate-400 text-sm font-mono">${this.escapeHtml(log.formattedTime.split(' ')[1] || log.formattedTime)}</div>
                <div class="col-span-2 flex items-center gap-2">
                    <div class="w-6 h-6 rounded-full bg-primary/20 flex items-center justify-center">
                        <span class="material-symbols-outlined text-primary text-[14px]">person</span>
                    </div>
                    <span class="text-sm font-medium text-slate-700 dark:text-slate-200">${this.escapeHtml(log.playerName)}</span>
                </div>
                <div class="col-span-2">
                    <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-bold ${methodClass}">
                        ${methodLabel}
                    </span>
                </div>
                <div class="col-span-3 flex items-center gap-2">
                    <div class="w-6 h-6 ${itemColor} rounded flex items-center justify-center text-[10px] text-white font-mono border border-slate-600">${itemAbbr}</div>
                    <span class="text-sm text-slate-600 dark:text-slate-300 truncate" title="${this.escapeHtml(log.itemType)}">${this.formatItemName(log.itemType)}</span>
                </div>
                <div class="col-span-1 text-right text-sm font-mono font-medium text-slate-600 dark:text-slate-300">x${log.amount}</div>
                <div class="col-span-2 text-right text-xs text-slate-500 dark:text-slate-400 font-mono">World: ${this.escapeHtml(log.world)}<br/><span class="opacity-60">${Math.round(log.x)}, ${Math.round(log.y)}, ${Math.round(log.z)}</span></div>
            </div>
        `;
    }

    getMethodClass(method) {
        switch (method) {
            case 'CREATIVE_INVENTORY':
                return 'bg-purple-100 text-purple-700 dark:bg-purple-500/20 dark:text-purple-400 border border-purple-200 dark:border-purple-500/30';
            case 'GIVE_COMMAND':
                return 'bg-orange-100 text-orange-700 dark:bg-orange-500/20 dark:text-orange-400 border border-orange-200 dark:border-orange-500/30';
            case 'GAMEMODE_CHANGE':
                return 'bg-blue-100 text-blue-700 dark:bg-blue-500/20 dark:text-blue-400 border border-blue-200 dark:border-blue-500/30';
            default:
                return 'bg-slate-100 text-slate-700 dark:bg-slate-700 dark:text-slate-300 border border-slate-200 dark:border-slate-600';
        }
    }

    getMethodLabel(method) {
        switch (method) {
            case 'CREATIVE_INVENTORY': return 'CREATIVE';
            case 'GIVE_COMMAND': return 'CMD_GIVE';
            case 'GAMEMODE_CHANGE': return 'GAMEMODE';
            default: return 'UNKNOWN';
        }
    }

    getItemAbbreviation(itemType) {
        const words = itemType.split('_');
        if (words.length >= 2) {
            return (words[0][0] + words[1][0]).toUpperCase();
        }
        return itemType.substring(0, 2).toUpperCase();
    }

    getItemColor(itemType) {
        const item = itemType.toLowerCase();
        if (item.includes('diamond')) return 'bg-cyan-600';
        if (item.includes('gold') || item.includes('golden')) return 'bg-yellow-600';
        if (item.includes('iron')) return 'bg-gray-400';
        if (item.includes('emerald')) return 'bg-emerald-600';
        if (item.includes('netherite')) return 'bg-gray-800';
        if (item.includes('oak') || item.includes('wood')) return 'bg-amber-800';
        if (item.includes('stone') || item.includes('cobble')) return 'bg-stone-500';
        if (item.includes('command')) return 'bg-purple-700';
        return 'bg-slate-600';
    }

    renderStats(stats) {
        document.getElementById('stat-total').textContent = stats.totalLogs.toLocaleString();
        document.getElementById('stat-players').textContent = stats.topPlayers.length.toLocaleString();

        const creativeCount = stats.methodBreakdown['Creative Inventory'] || 0;
        const giveCount = stats.methodBreakdown['Give Command'] || 0;

        document.getElementById('stat-creative').textContent = creativeCount.toLocaleString();
        document.getElementById('stat-give').textContent = giveCount.toLocaleString();
    }

    updatePagination(data) {
        this.totalPages = data.totalPages || 1;

        const start = data.logs.length > 0 ? (this.currentPage * this.pageSize) + 1 : 0;
        const end = start + data.logs.length - (data.logs.length > 0 ? 1 : 0);

        document.getElementById('showing-start').textContent = start;
        document.getElementById('showing-end').textContent = end;
        document.getElementById('total-count').textContent = data.totalLogs;

        document.getElementById('prev-page').disabled = this.currentPage === 0;
        document.getElementById('next-page').disabled = this.currentPage >= this.totalPages - 1;
    }

    applyFilters() {
        this.filters.player = document.getElementById('filter-player').value.trim();
        this.filters.method = document.getElementById('filter-method').value;
        this.currentPage = 0;
        this.loadLogs();
    }

    clearFilters() {
        document.getElementById('filter-player').value = '';
        document.getElementById('filter-method').value = '';
        this.filters = { player: '', method: '' };
        this.currentPage = 0;
        this.loadLogs();
    }

    prevPage() {
        if (this.currentPage > 0) {
            this.currentPage--;
            this.loadLogs();
        }
    }

    nextPage() {
        if (this.currentPage < this.totalPages - 1) {
            this.currentPage++;
            this.loadLogs();
        }
    }

    async handleClearLogs() {
        if (!confirm('Are you sure you want to delete ALL logs? This action cannot be undone.')) {
            return;
        }

        try {
            const response = await fetch('/api/logs', {
                method: 'DELETE'
            });

            if (response.status === 401) {
                this.showLoginForm();
                return;
            }

            const data = await response.json();
            if (data.success) {
                this.currentPage = 0;
                this.loadData();
                alert('All logs have been cleared!');
            } else {
                alert('Failed to clear logs: ' + data.message);
            }
        } catch (error) {
            console.error('Failed to clear logs:', error);
            alert('Failed to clear logs. Please try again.');
        }
    }

    formatItemName(name) {
        return name.replace(/_/g, ' ').toLowerCase()
            .split(' ')
            .map(word => word.charAt(0).toUpperCase() + word.slice(1))
            .join(' ');
    }

    escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
}

// Password visibility toggle
function togglePasswordVisibility() {
    const input = document.getElementById('login-password');
    const icon = document.getElementById('password-toggle-icon');

    if (input.type === 'password') {
        input.type = 'text';
        icon.textContent = 'visibility_off';
    } else {
        input.type = 'password';
        icon.textContent = 'visibility';
    }
}

// Initialize app when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    window.app = new FoliaTrackerApp();
});
